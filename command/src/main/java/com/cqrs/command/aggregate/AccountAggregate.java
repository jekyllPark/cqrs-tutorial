package com.cqrs.command.aggregate;

import com.cqrs.command.commands.AccountCreationCommand;
import com.cqrs.command.commands.DepositMoneyCommand;
import com.cqrs.command.commands.WithdrawMoneyCommand;
import com.cqrs.events.AccountCreationEvent;
import com.cqrs.events.DepositMoneyEvent;
import com.cqrs.events.WithdrawMoneyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@RequiredArgsConstructor
@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private String holderId;
    private Long balance;

    @CommandHandler
    public AccountAggregate(AccountCreationCommand command) {
        log.debug("handling {}", command);
        apply(new AccountCreationEvent(command.getHolderId(), command.getAccountId()));
    }

    @EventSourcingHandler
    protected void createAccount(AccountCreationEvent event) {
        log.debug("applying {}", event);
        this.accountId = event.getAccountId();
        this.holderId = event.getHolderId();
        this.balance = 0L;
    }

    @CommandHandler
    protected void depositMoney(DepositMoneyCommand command) {
        log.debug("handling {}", command);
        if (command.getAmount() <= 0) throw new IllegalStateException("The amount must be greater and equal than 0");
        apply(new DepositMoneyEvent(command.getHolderId(), command.getAccountId(), command.getAmount()));
    }

    @EventSourcingHandler
    protected void depositMoney(DepositMoneyEvent event) {
        log.debug("applying {}", event);
        this.balance += event.getAmount();
    }

    @CommandHandler
    protected void withdrawMoney(WithdrawMoneyCommand command) {
        log.debug("handling {}", command);
        if (this.balance - command.getAmount() < 0) throw new IllegalStateException("The balance is insufficient");
        else if (command.getAmount() <= 0) throw new IllegalStateException("The amount must be greater and equal than 0");
        apply(new WithdrawMoneyEvent(command.getHolderId(), command.getAccountId(), command.getAmount()));
    }

    @EventSourcingHandler
    protected void withdrawMoney(WithdrawMoneyEvent event) {
        log.debug("applying {}", event);
        this.balance -= event.getAmount();
    }
}
