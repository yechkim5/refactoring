package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;

        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer()
                        + System.lineSeparator());

        for (Performance performance : invoice.getPerformances()) {

            // add volume credits for this performance
            volumeCredits += getVolumeCredits(performance);

            // print line for this order
            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(getAmount(performance)),
                    performance.getAudience()));

            totalAmount += getAmount(performance);
        }

        result.append(String.format(
                "Amount owed is %s%n",
                usd(totalAmount)));
        result.append(String.format(
                "You earned %s credits%n",
                volumeCredits));

        return result.toString();
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Calculates the amount owed for a single performance.
     *
     * @param performance the performance for which to calculate the amount
     * @return the amount owed in cents
     * @throws RuntimeException if the play type is unknown
     */
    private int getAmount(Performance performance) {
        final Play play = getPlay(performance);
        int result = 0;

        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE
                        * performance.getAudience();
                break;
            default:
                throw new RuntimeException(
                        String.format("unknown type: %s", play.getType()));
        }

        return result;
    }

    /**
     * Calculates the volume credits earned for a single performance.
     *
     * @param performance the performance for which to calculate volume credits
     * @return the volume credits earned for this performance
     */
    private int getVolumeCredits(Performance performance) {
        int result = Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD,
                0);

        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Formats an amount expressed in cents as a US dollar currency string.
     *
     * @param amountInCents the amount in cents
     * @return the formatted amount in US dollars
     */
    private String usd(int amountInCents) {
        final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return numberFormat.format(amountInCents / Constants.PERCENT_FACTOR);
    }
}
