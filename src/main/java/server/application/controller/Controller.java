package server.application.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.application.model.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Controller {
    Map<String, Stocks> stocks = new ConcurrentHashMap<>();
    Map<String, Client> clients = new ConcurrentHashMap<>();

    @RequestMapping("/stocks/register/{companyName}/{amount}/{price}")
    public String registerCompany(@PathVariable String companyName, @PathVariable Long amount, @PathVariable Double price) {
        if (companyName.contains(":")) {
            return "Invalid company name! Try another.";
        }

        var companyStocks = stocks.putIfAbsent(companyName, new Stocks(companyName, amount, price));

        if (companyStocks == null) {
            return "Company " + companyName + " added.";
        }

        return "Sorry. Already exists.";
    }

    @RequestMapping("/stocks/info/{companyName}")
    public String getCompany(@PathVariable String companyName) {
        var companyStocks = stocks.get(companyName);

        if (companyStocks == null) {
            return "Sorry. No such company.";
        }

        return companyStocks.toString();
    }

    @RequestMapping("/stocks/add/{companyName}/{change}")
    public String addStocks(@PathVariable String companyName, @PathVariable Long change) {
        return stocksAmountChange(companyName, change);
    }

    @RequestMapping("/stocks/remove/{companyName}/{change}")
    public String removeStocks(@PathVariable String companyName, @PathVariable Long change) {
        return stocksAmountChange(companyName, -change);
    }

    @RequestMapping("/stocks/inc/{companyName}/{change}")
    public String incStocksPrice(@PathVariable String companyName, @PathVariable Double change) {
        return stocksPriceChange(companyName, change);
    }

    @RequestMapping("/stocks/dec/{companyName}/{change}")
    public String decStocksPrice(@PathVariable String companyName, @PathVariable Double change) {
        return stocksPriceChange(companyName, -change);
    }

    @RequestMapping("/client/register/{login}")
    public String clientRegister(@PathVariable String login) {
        var client = clients.putIfAbsent(login, new Client(login));

        if (client == null) {
            return "User registered!";
        }

        return "User with such login was already created!";
    }

    @RequestMapping("/client/add/{login}/{change}")
    public String clientAddMoney(@PathVariable String login, @PathVariable Long change) {
        var client = clients.get(login);

        if (client == null) {
            return "No such a client";
        }

        double oldMoney = client.getMoney();

        client.addMoney(change);

        return "Success. Old value is " + oldMoney + ", changed to " + client.getMoney() + ".";
    }

    @RequestMapping("/client/info/{login}")
    public String clientInfo(@PathVariable String login) {
        var client = clients.get(login);

        if (client == null) {
            return "No such a client";
        }

        return client.toString();
    }

    @RequestMapping("/client/buy/{login}/{companyName}/{amount}")
    public String clientBuyStocks(@PathVariable String login, @PathVariable String companyName, @PathVariable Long amount) {
        var client = clients.get(login);

        if (client == null) {
            return "No such a client";
        }

        var companyStocks = stocks.get(companyName);

        if (companyStocks == null) {
            return "Sorry. No such company.";
        }

        if (companyStocks.getAmount() < amount) {
            return "No enough stocks! Try later!";
        }

        if (companyStocks.getPrice() * amount > client.getMoney()) {
            return "No enough money! Try later!";
        }

        companyStocks.changeAmount(-amount);
        client.buyStocks(new Stocks(companyName, amount, companyStocks.getPrice()));

        return "Success";
    }

    @RequestMapping("/client/sell/{login}/{companyName}/{amount}")
    public String clientSellStocks(@PathVariable String login, @PathVariable String companyName, @PathVariable Long amount) {
        var client = clients.get(login);

        if (client == null) {
            return "No such a client";
        }

        var companyStocks = stocks.get(companyName);

        if (companyStocks == null) {
            return "Sorry. No such company.";
        }

        if (client.sellStocks(new Stocks(companyName, amount, companyStocks.getPrice()))) {
            companyStocks.changeAmount(amount);
            return "Success";
        } else {
            return "Client has no this stocks";
        }
    }

    @RequestMapping("/client/money/{login}")
    public String clientSellStocks(@PathVariable String login) {
        var client = clients.get(login);

        if (client == null) {
            return "No such a client";
        }

        double money = client.getMoney();

        for (Stocks companyStocks: client.getStocks()) {
            if (stocks.containsKey(companyStocks.getName())) {
                money += companyStocks.getAmount() * stocks.get(companyStocks.getName()).getPrice();
            }
        }

        return String.valueOf(money);
    }

    private String stocksAmountChange(String companyName, long change) {
        var companyStocks = stocks.get(companyName);

        if (companyStocks == null) {
            return "Sorry. No such company.";
        }

        long oldAmount = companyStocks.getAmount();

        companyStocks.changeAmount(change);

        return "Stocks for " + companyName + " added. Old amount was " + oldAmount + ", new is " + companyStocks.getAmount();
    }

    private String stocksPriceChange(String companyName, Double change) {
        var companyStocks = stocks.get(companyName);

        if (companyStocks == null) {
            return "Sorry. No such company.";
        }

        double oldPrice = companyStocks.getPrice();

        companyStocks.changePrice(change);

        return "Price for " + companyName + " changed. Old price was " + oldPrice + ", new is " + companyStocks.getPrice() + ".";
    }
}
