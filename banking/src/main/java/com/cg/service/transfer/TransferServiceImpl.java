package com.cg.service.transfer;

import com.cg.model.Transfer;

import java.util.List;
import java.util.Optional;

public class TransferServiceImpl implements ITransferService{
    @Override
    public List<Transfer> findAll() {
        return null;
    }

    @Override
    public Optional<Transfer> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Boolean existById(Long id) {
        return null;
    }

    @Override
    public Transfer save(Transfer transfer) {
        return null;
    }

    @Override
    public void delete(Transfer transfer) {

    }

    @Override
    public void deleteById(Long id) {

    }
}
