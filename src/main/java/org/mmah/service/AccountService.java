package org.mmah.service;

import org.mmah.model.Account;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/29/13
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AccountService {
    Account getById(long id);
    Account getByName(String name);
    List<Account> findByName(String name);
}
