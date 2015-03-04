package netflix.karyon;

import com.google.inject.CreationException;
import com.google.inject.spi.Message;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.annotations.Bootstrap;

/**
 * An application runner which consumes a main class annotated with governator's {@link Bootstrap} annotations.
 *
 * This is shorthand for:
 *
 <PRE>
     com.netflix.karyon.forApplication(MyApp.class).startAndWaitTillShutdown()
 </PRE>
 *
 * where the name of the Application class is passed as the argument to the main method.
 *
 * This is useful while creating standard packaging scripts where the main class for starting the JVM remains the same
 * and the actual application class differs from one application to another.
 *
 * If you are bootstrapping karyon programmatically, it is better to use {@code Karyon} directly.
 *
 * @author Nitesh Kant
 */
public class KaryonRunner {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + KaryonRunner.class.getCanonicalName() + " <main classs name>");
            System.exit(-1);
        }

        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);

        try {
            Karyon.forApplication(Class.forName(mainClassName), (BootstrapModule[]) null)
                  .startAndWaitTillShutdown();
        } catch (@SuppressWarnings("UnusedCatchParameter") ClassNotFoundException e) {
            System.err.println("Main class: " + mainClassName + " not found.");
            System.exit(-1);
        } catch (CreationException e) {
            System.err.println("Injection error while starting karyon server. Messages follow:");
            for (Message msg : e.getErrorMessages()) {
                System.err.printf("ErrorMessage: %s\n", getErrorCauseMessages(e.getCause(), 4));
                if (msg.getCause() != null) {
                    msg.getCause().printStackTrace();
                }
            }
            System.exit(-1);
        } catch (Exception e) {
            System.err.println("Error while starting karyon server. msg=" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        // In case we have non-daemon threads running
        System.exit(0);
    }

    private static String getErrorCauseMessages(Throwable error, int depth) {
        String fullMessage = "";
        Throwable cause = error;
        for (int i=0; i<depth; i++) {
            if (cause == null) {
                break;
            }
            fullMessage = fullMessage + String.format("cause%s=\"%s\", ", i, cause.getMessage());
            cause = cause.getCause();
        }
        return fullMessage;
    }
}
