package com.cg.controller;


import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Transfer;
import com.cg.model.Withdraw;
import com.cg.service.customer.ICustomerService;
import com.cg.service.deposit.IDepositService;
import com.cg.service.withdraw.IWithdrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IDepositService depositService;

    @Autowired
    private IWithdrawService withdrawService;


    @GetMapping
    public String showListPage(Model model) {

        List<Customer> customers = customerService.findAllByDeletedIsFalse();

        model.addAttribute("customers", customers);

        return "customer/list";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/create";
    }

    @GetMapping("/update/{customerId}")
    public String showUpdatePage(@PathVariable Long customerId, Model model) {

        Optional<Customer> customerOptional = customerService.findById(customerId);
        if (!customerOptional.isPresent()) {
            return "error/404";
        }

        model.addAttribute("customer", customerOptional.get());

        return "customer/edit";
    }

    @GetMapping("/search")
    public String search(@RequestParam("searchKey") String searchKey, Model model) {

        searchKey = "%" + searchKey + "%";

        List<Customer> customers = customerService.findAllByFullNameLikeOrEmailLikeOrPhoneLikeOrAddressLike(searchKey, searchKey, searchKey, searchKey);

        model.addAttribute("customers", customers);

        return "customer/list";
    }

    @GetMapping("/deposit/{customerId}")
    public String showDepositPage(@PathVariable Long customerId, Model model) {

        Optional<Customer> customerOptional = customerService.findById(customerId);

        if (!customerOptional.isPresent()) {
            return "error/404";
        }

        model.addAttribute("customer", customerOptional.get());

        Deposit deposit = new Deposit();
        deposit.setCustomer(customerOptional.get());

        model.addAttribute("deposit", deposit);

        return "customer/deposit";
    }

    @GetMapping("/withdraw/{customerId}")
    public String showWithdrawPage(@PathVariable Long customerId, Model model) {
        Optional<Customer> customerOptional = customerService.findById((customerId));
        if (!customerOptional.isPresent()){
            return "error/404";
        }
        model.addAttribute("customer", customerOptional.get());

        Withdraw withdraw = new Withdraw();
        withdraw.setCustomer((customerOptional.get()));

        model.addAttribute("withdraw", withdraw);

        return "customer/withdraw";
    }

    @GetMapping("/transfer/{senderId}")
    public String showTransferPage(@PathVariable Long senderId, Model model) {

        Optional<Customer> senderOptional = customerService.findById(senderId);

        if (!senderOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("messages", "Sender not found");
        }
        else {
            Customer sender = senderOptional.get();

            Transfer transfer = new Transfer();
            transfer.setSender(sender);

            model.addAttribute("transfer", transfer);

            List<Customer> recipients = customerService.findAllByIdNotAndDeletedIsFalse(senderId);

            model.addAttribute("recipients", recipients);
        }

        return "customer/transfer";
    }

    @GetMapping("/delete/{id}")
    public String showDeletePage(Model model, @PathVariable Long id) {

        Optional<Customer> customerOptional = customerService.findById(id);

        if (!customerOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Customer ID invalid");
            model.addAttribute("customer", new Customer());
        }
        else {
            Customer customer = customerOptional.get();

            if (customer.isDeleted()) {
                model.addAttribute("error", true);
                model.addAttribute("message", "This Customer is suspended");
            }
            else {
                model.addAttribute("error", null);
            }

            model.addAttribute("customer", customer);
        }

        return "customer/delete";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Customer customer, Model model) {

        customer.setId(null);
        customer.setBalance(BigDecimal.ZERO);
        customerService.save(customer);

        model.addAttribute("success", true);
        model.addAttribute("messages", "Create successfully ");

        return "customer/create";
    }


    @PostMapping("/update/{customerId}")
    public String update(@PathVariable Long customerId, @ModelAttribute Customer customer) {

        Optional<Customer> customerOptional = customerService.findById(customerId);
        if (!customerOptional.isPresent()) {
            return "error/404";
        }

        customer.setBalance(customerOptional.get().getBalance());
        customer.setId(customerId);
        customerService.save(customer);

        return "redirect:/customers";
    }

    @PostMapping("/deposit/{customerId}")
    public String deposit(@PathVariable Long customerId, @ModelAttribute Deposit deposit, Model model) {

        Optional<Customer> customerOptional = customerService.findById(customerId);
        if (!customerOptional.isPresent()) {
            return "error/404";
        }

        Customer customer = customerOptional.get();
        BigDecimal currentBalance = customer.getBalance();
        BigDecimal transactionAmount = deposit.getTransactionAmount();
        BigDecimal newBalance = currentBalance.add(transactionAmount);

        customer.setBalance(newBalance);

        deposit.setCustomer(customer);

        customerService.save(customer);
        depositService.save(deposit);

        deposit.setTransactionAmount(BigDecimal.ZERO);

        model.addAttribute("deposit", deposit);

        return "customer/deposit";
    }

    @PostMapping("/withdraw/{customerId}")
    public String withdraw(@PathVariable Long customerId, @ModelAttribute Withdraw withdraw, Model model) {
        Optional<Customer> customerOptional = customerService.findById(customerId);
        if (!customerOptional.isPresent()) {
            return "error/404";
        } else {
            Customer customer = customerOptional.get();
            BigDecimal currentBalance = customer.getBalance();
            BigDecimal transactionAmount = withdraw.getTransactionAmount();
            if (currentBalance.compareTo(transactionAmount) < 0) {
                model.addAttribute("error", true);
                model.addAttribute("messages", "transactionAmount must be less than current balance ");
            }
            else {
                BigDecimal newBalance = currentBalance.subtract(transactionAmount);
                customer.setBalance(newBalance);
                withdraw.setCustomer(customer);

                customerService.save(customer);
                withdrawService.save(withdraw);

                withdraw.setTransactionAmount(BigDecimal.ZERO);

                model.addAttribute("withdraw", withdraw);

            }
        }
        return "customer/withdraw";
    }

    @PostMapping("/transfer/{senderId}")
    public String doTransfer(@PathVariable Long senderId, @ModelAttribute Transfer transfer, Model model) {

        Optional<Customer> senderOptional = customerService.findById(senderId);

        List<Customer> recipients = customerService.findAllByIdNotAndDeletedIsFalse(senderId);

        model.addAttribute("recipients", recipients);

        if (!senderOptional.isPresent()) {
            model.addAttribute("transfer", transfer);

            model.addAttribute("error", true);
            model.addAttribute("messages", "Sender not valid");

            return "customer/transfer";
        }

        Long recipientId = transfer.getRecipient().getId();

        Optional<Customer> recipientOptional = customerService.findById(recipientId);

        if (!recipientOptional.isPresent()) {
            model.addAttribute("transfer", transfer);

            model.addAttribute("error", true);
            model.addAttribute("messages", "Recipient not valid");

            return "customer/transfer";
        }

        if (senderId.equals(recipientId)) {
            model.addAttribute("error", true);
            model.addAttribute("messages", "Sender ID must be different from Recipient ID");

            return "customer/transfer";
        }

        BigDecimal senderCurrentBalance = senderOptional.get().getBalance();

        BigDecimal transferAmount = transfer.getTransferAmount();
        long fees = 10L;
        BigDecimal feesAmount = transferAmount.multiply(BigDecimal.valueOf(fees)).divide(BigDecimal.valueOf(100));
        BigDecimal transactionAmount = transferAmount.add(feesAmount);

        if (senderCurrentBalance.compareTo(transactionAmount) < 0) {
            model.addAttribute("error", true);
            model.addAttribute("messages", "Sender balance not enough to transfer");

            return "customer/transfer";
        }

        transfer.setSender(senderOptional.get());
        transfer.setFees(fees);
        transfer.setFeesAmount(feesAmount);
        transfer.setTransactionAmount(transactionAmount);

        customerService.transfer(transfer);

        transfer.setTransferAmount(BigDecimal.ZERO);
        transfer.setTransactionAmount(BigDecimal.ZERO);

        model.addAttribute("transfer", transfer);

        model.addAttribute("success", true);
        model.addAttribute("messages", "Transfer success");

        return "customer/transfer";
    }

    @PostMapping("/delete/{customerId}")
    public String softDelete(@PathVariable Long customerId, Model model) {

        Optional<Customer> customerOptional = customerService.findById(customerId);

        if (!customerOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Customer ID invalid");
            model.addAttribute("customer", new Customer());
        }
        else {
            Customer customer = customerOptional.get();
            customer.setDeleted(true);
            customerService.save(customer);

            model.addAttribute("error", false);
            model.addAttribute("customer", customer);
        }

        return "customer/list";
    }
}
