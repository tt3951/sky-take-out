package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AddressBookServiceImpl implements AddressBookService {


    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> addressBookList = addressBookMapper.list(userId);
        return addressBookList;
    }


    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {

        return addressBookMapper.getById(id);
    }

    @Override
    public void update(AddressBook addressBook) {

        addressBookMapper.update(addressBook);

    }

    @Override
    public void setDefault(AddressBook addressBook) {

        //首先用户的地址全部设置为非默认地址,根据userId
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.updateIsDefaultByUserId(addressBook);
        //再将当前地址修改为默认地址,根据id，复用update
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);


    }
}
