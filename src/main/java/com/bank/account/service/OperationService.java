package com.bank.account.service;

import com.bank.account.exception.UnauthorizedOperationException;
import com.bank.account.model.Account;
import com.bank.account.model.Operation;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.OperationRepository;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

import static com.bank.account.util.CurrencyUtil.convertAmount;
import static com.bank.account.util.ValidatorUtil.validate;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.Validate.notEmpty;

@Service
public class OperationService {

    private AccountRepository accountRepository;
    private OperationRepository operationRepository;

    @Inject
    public OperationService(AccountRepository accountRepository, OperationRepository operationRepository) {
        this.accountRepository = requireNonNull(accountRepository);
        this.operationRepository = requireNonNull(operationRepository);
    }
    
    
    /**
     * history of operations 
     * @param accountId
     * @param startOperationDate
     * @param endOperationDate
     * @return
     */
    public List<Operation> findOperations(String accountId,
                                          Instant startOperationDate,
                                          Instant endOperationDate) {

        return operationRepository
            .findOperationsByAccountIdAndDateBetweenOrderByDateDesc(notEmpty(accountId),
                requireNonNull(startOperationDate),
                requireNonNull(endOperationDate));
    }
    
    /**
     * save operation (deposit or withdrawl)
     * @param operation
     */
    public void saveOperation(Operation operation) {
        validate(operation);

        Account account = getAccount(operation);

        double newAmountBeforeValidationOperation = getNewAmountBeforeValidationOperation(account, operation);

        if (!isOperationAllowed(account, newAmountBeforeValidationOperation)) {
            throw new UnauthorizedOperationException(account, operation);
        }

        account.setAmount(newAmountBeforeValidationOperation);

        accountRepository.save(account);
        operationRepository.save(requireNonNull(operation));
    }
    
    
    /**
     * get Account by id 
     * @param operation
     * @return
     */
    private Account getAccount(Operation operation) {
        return requireNonNull(accountRepository.findOne(operation.getAccountId()));
    }
    /**
     * calculate  new Amount Before Validation Operation
     * @param account
     * @param operation
     * @return
     */
    private double getNewAmountBeforeValidationOperation(Account account, Operation operation) {

        double accountAmount = account.getAmount();
        double operationAmount = operation.getAmount();

        Currency accountCurrency = account.getCurrency();
        Currency operationCurrency = operation.getCurrency();

        double
            operationAmountWithAccountCurrency =
            convertAmount(operationAmount, operationCurrency, accountCurrency);

        return accountAmount + operationAmountWithAccountCurrency;
    }
    
    /**
     * check if Operation is Allowed 
     * @param account
     * @param new Account Amount after operation (save or withdrawal)
     * @return
     */
    private boolean isOperationAllowed(Account account, double newAmountBeforeValidationOperation) {
        return account.isAllowNegativeAmount() || newAmountBeforeValidationOperation >= 0;
    }
}
