package services.codegen

import models.codegen.Engine
import models.codegen.Engine._

object PlanProvider {
  def explainSupported(implicit engine: Engine) = engine match {
    case MySQL | PostgreSQL => true
    case _ => false
  }

  def explain(implicit engine: Engine) = engine match {
    case PostgreSQL => "\"explain (costs, verbose, format json) \" + sql"
    case MySQL => "\"explain format=json \" + sql"
    case _ => ""
  }

  def analyzeSupported(implicit engine: Engine) = engine match {
    case PostgreSQL => true
    case _ => false
  }

  def analyze(implicit engine: Engine) = engine match {
    case PostgreSQL => "\"explain (analyze, costs, verbose, buffers, format json) \" + sql"
    case _ => ""
  }
}
