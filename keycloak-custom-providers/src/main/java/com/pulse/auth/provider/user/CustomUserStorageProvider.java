package com.pulse.auth.provider.user;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
        UserQueryProvider {

        private static final Logger log = LoggerFactory.getLogger(CustomUserStorageProvider.class);
        private KeycloakSession ksession;
        private ComponentModel model;

        public CustomUserStorageProvider(KeycloakSession ksession, ComponentModel model) {
            this.ksession = ksession;
            this.model = model;
        }

        @Override
        public void close() { log.info("[I30] close()");}

        @Override
        public UserModel getUserById(String id, RealmModel realm) {
            log.info("[I35] getUserById({})",id);
            StorageId sid = new StorageId(id);
            return getUserByUsername(sid.getExternalId(),realm);
        }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        log.info("[I41] getUserByUsername({})",username);
        try ( Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select username, firstName,lastName, email, birthDate from users where username = ?");
            st.setString(1, username);
            st.execute();
            ResultSet rs = st.getResultSet();
            if ( rs.next()) {
                return mapUser(realm,rs);
            }
            else {
                return null;
            }
        }
        catch(SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(),ex);
        }
    }
}
