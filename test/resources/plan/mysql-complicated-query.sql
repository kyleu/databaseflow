  SELECT * FROM (SELECT
    ol.id as offer_link_id,
      null as `publisher_uuid`,
      null as `publisher_subid`,
    network_id,
    n.override_network_source_distribution network_override_network_source_distribution,
    platform_id,
    country_iso,
    clicks_needed,
    td.name AS tracking_domain,
    ol.url,
    n.url_append,
    ol.offer_id
  FROM offer_link ol
  JOIN offer o ON o.id=ol.offer_id
  JOIN network n ON n.id=ol.network_id AND n.override_network_source_distribution = 0
    LEFT JOIN tracking_domain td ON n.tracking_domain_id=td.id
    LEFT JOIN (
                SELECT
                  v.dma_key AS dma_code, li.offer_link_id
                FROM attribute_target AT JOIN target t ON AT.target_id = t.id
                  JOIN attribute a ON a.id = AT.attribute_id
                  JOIN campaign c ON t.id = c.target_id
                  JOIN v_attribute_dma_city v ON a.key = v.city_key
                  JOIN line_item li ON li.campaign_id=c.id
                WHERE attribute_type_id = 4
                GROUP BY dma_key, offer_link_id
              ) dma_offer_link ON dma_offer_link.offer_link_id = ol.id
   LEFT JOIN (
              SELECT a.key as city, li.offer_link_id
              FROM attribute_target at JOIN target t ON at.target_id=t.id
              JOIN attribute a ON a.id=at.attribute_id
              JOIN campaign c ON t.id=c.target_id
              JOIN line_item li ON li.campaign_id=c.id
             WHERE attribute_type_id=4
             AND  li.offer_link_id is not NULL
             GROUP BY a.key, offer_link_id
          ) city_offer_link ON city_offer_link.offer_link_id = ol.id
    JOIN (
       SELECT
         offer_link_id,
         SUM(conversions)                      AS conversions,
         SUM(conversions) / 1 - SUM(clicks) AS clicks_needed
       FROM rollup_offer_link_hour rohr
         JOIN offer_link ol ON ol.id = rohr.offer_link_id
       WHERE hour > @date_start - INTERVAL 12 HOUR
       GROUP BY offer_link_id
       ) ro
    ON ro.offer_link_id = ol.id
        WHERE ol.active IN (1, 4)
        AND ro.clicks_needed > 0
        AND ro.conversions > 0
        AND o.pay_method='CPI'
      AND country_iso IS NOT NULL
      AND incent_allowed = 0
          AND city is NULL
          AND dma_code is NULL
        ) non_transparent_network
        UNION
        SELECT * FROM (SELECT
    offer_link_id,
      publisher_uuid,
      ro.publisher_subid,
    network_id,
    n.override_network_source_distribution network_override_network_source_distribution,
    platform_id,
    country_iso,
    clicks_needed,
    td.name AS tracking_domain,
    ol.url,
    n.url_append,
    ol.offer_id
  FROM offer_link ol
  JOIN network n ON n.id=ol.network_id AND n.override_network_source_distribution = 1
  JOIN offer o ON o.id=ol.offer_id
LEFT JOIN tracking_domain td ON n.tracking_domain_id=td.id
    JOIN (
      #-------
SELECT
    rchr.offer_link_id,
    p.uuid publisher_uuid,
        rchr.publisher_subid,
    SUM(rchr.clicks) as clicks,
        SUM(rchr.clicks) + IFNULL(ct.clicks, 0) as clicks_CRD,
    SUM(rchr.conversions)                           AS conversions,
    GREATEST(SUM(rchr.conversions) / 1 - (SUM(rchr.clicks) + IFNULL(ct.clicks, 0)),0) AS clicks_needed
  FROM rollup_crd_hour rchr
    JOIN publisher p ON p.id = rchr.publisher_id AND p.crd = 0
    JOIN offer_link ol ON ol.id = rchr.offer_link_id
    Join network n ON n.id = ol.network_id AND n.override_network_source_distribution = 1
    LEFT JOIN (SELECT publisher_uuid, publisher_subid, offer_link_id, count(*) clicks FROM appthis_v2.crd_traffic
          where date > @date_start - INTERVAL 12 HOUR
          group by  publisher_uuid, publisher_subid, offer_link_id) ct
     ON  ct.publisher_uuid = p.uuid AND ct.publisher_subid = rchr.publisher_subid AND ct.offer_link_id = rchr.offer_link_id
  WHERE rchr.hour > @date_start - INTERVAL 12 HOUR
  GROUP BY p.uuid, rchr.publisher_subid, rchr.offer_link_id

      #-------
       ) ro
    ON ro.offer_link_id = ol.id
      WHERE ol.active IN (1, 3, 4)
        AND ro.clicks_needed > 0
        AND ro.conversions > 0
      AND country_iso IS NOT NULL
      AND incent_allowed = 0
      AND o.pay_method='CPI'
      ) transparent_network
