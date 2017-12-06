package util.metrics

import java.lang.management.ManagementFactory

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jvm._
import nl.grons.metrics.scala.{MetricName, InstrumentedBuilder}

object Instrumented {
  val projectId = "databaseflow"

  val metricRegistry = new MetricRegistry()
  metricRegistry.register(s"$projectId.jvm.buffer-pools", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer))
  metricRegistry.register(s"$projectId.jvm.class-loading", new ClassLoadingGaugeSet())
  metricRegistry.register(s"$projectId.jvm.fd.usage", new FileDescriptorRatioGauge())
  metricRegistry.register(s"$projectId.jvm.gc", new GarbageCollectorMetricSet())
  metricRegistry.register(s"$projectId.jvm.memory", new MemoryUsageGaugeSet())
  metricRegistry.register(s"$projectId.jvm.thread-states", new ThreadStatesGaugeSet())
}

trait Instrumented extends InstrumentedBuilder {
  override lazy val metricBaseName = MetricName(s"${Instrumented.projectId}.${getClass.getSimpleName.replaceAllLiterally("$", "")}")
  override val metricRegistry = Instrumented.metricRegistry
}
