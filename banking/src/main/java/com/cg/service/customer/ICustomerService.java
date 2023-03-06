package com.cg.service.customer;

import com.cg.model.Customer;
import com.cg.model.Transfer;
import com.cg.service.IGeneralService;

import java.util.List;

public interface ICustomerService extends IGeneralService<Customer> {

    List<Customer> findAllByFullNameLikeOrEmailLikeOrPhoneLikeOrAddressLike(String fullName, String email, String phone, String address);

    List<Customer> findAllByIdNotAndDeletedIsFalse(Long senderId);

    Transfer transfer(Transfer transfer);

    List<Customer> findAllByIdNot(Long senderId);

    List<Customer> findAllByDeletedIsFalse();
}
