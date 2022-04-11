package server.application.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.application.model.Stocks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Controller {
    Map<String, Stocks> stocks = new ConcurrentHashMap<>();

    @RequestMapping("/register/{companyName}")
    public String registerCompany(@PathVariable String companyName) {
        var companyStocks = stocks.putIfAbsent(companyName, new Stocks(companyName));

        if (companyStocks == null) {
            return "Company " + companyName + " added.";
        }

        return "Sorry. Already exists.";
    }

    @RequestMapping("/get/{companyName}")
    public String getCompany(@PathVariable String companyName) {
        var companyStocks = stocks.get(companyName);

        if (companyStocks == null) {
            return "Sorry. No such company.";
        }

        return companyStocks.toString();
    }

    @RequestMapping("/add/{companyName}/{change}")
    public String addStocks(@PathVariable String companyName, @PathVariable Long change) {
        return stocksAmountChange(companyName, change);
    }

    @RequestMapping("/remove/{companyName}/{change}")
    public String removeStocks(@PathVariable String companyName, @PathVariable Long change) {
        return stocksAmountChange(companyName, -change);
    }

    @RequestMapping("/inc/{companyName}/{change}")
    public String incStocksPrice(@PathVariable String companyName, @PathVariable Double change) {
        return stocksPriceChange(companyName, change);
    }

    @RequestMapping("/dec/{companyName}/{change}")
    public String decStocksPrice(@PathVariable String companyName, @PathVariable Double change) {
        return stocksPriceChange(companyName, -change);
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
