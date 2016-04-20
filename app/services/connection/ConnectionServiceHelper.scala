package services.connection

trait ConnectionServiceHelper
    extends StartHelper
    with DataHelper
    with TraceHelper
    with DetailHelper
    with QueryHelper
    with PlanHelper
    with SqlHelper { this: ConnectionService =>

}
