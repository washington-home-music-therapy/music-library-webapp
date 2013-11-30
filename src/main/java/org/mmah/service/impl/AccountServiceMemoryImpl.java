package org.mmah.service.impl;

import com.google.common.collect.ConcurrentHashMultiset;
import org.mmah.model.Account;
import org.mmah.service.AccountService;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/29/13
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccountServiceMemoryImpl implements AccountService {
    private final ConcurrentHashMap<Long, Account> accountById = new ConcurrentHashMap();

    @Override
    public Account getById(long id) {
        return accountById.get(id);
    }

    @Override
    public Account getByName(String name) {
        return null;
    }

    @Override
    public List<Account> findByName(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
