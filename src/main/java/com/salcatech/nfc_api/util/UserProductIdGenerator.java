package com.salcatech.nfc_api.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

public class UserProductIdGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        // 10 karakterlik rastgele ID
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, 10);
    }
}
