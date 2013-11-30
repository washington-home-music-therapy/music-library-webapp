package org.mmah.service;

import org.mmah.model.Account;
import org.mmah.model.Library;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/29/13
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface LibraryService {
    Library findById(long id);
    List<Library> findByAccount(Account account);
    List<Library> findByName(String name);
}
