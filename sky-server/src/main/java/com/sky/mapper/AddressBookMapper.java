package com.sky.mapper;


import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {


    @Select("select * from address_book where user_id = #{userId}")
    List<AddressBook> list(Long userId);
}
