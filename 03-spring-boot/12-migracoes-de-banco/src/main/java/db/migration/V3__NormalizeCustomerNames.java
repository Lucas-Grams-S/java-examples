package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** SB12-11: migração em Java (versão 3 entre as SQL; a repetível R__ roda depois de todas as versionadas). */
public class V3__NormalizeCustomerNames extends BaseJavaMigration {
    @Override public void migrate(Context context) throws Exception {
        try (var st = context.getConnection().createStatement()) {
            st.executeUpdate("update orders set customer = trim(customer)");
        }
    }
}
