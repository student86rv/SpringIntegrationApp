package ua.epam.springIntegrationApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import ua.epam.springIntegrationApp.model.DeliveryType;
import ua.epam.springIntegrationApp.model.Package;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class Application {

    private static AtomicInteger count = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        DeliveryService service = ctx.getBean(DeliveryService.class);
        for (int i = 1; i <= 10; i++) {
            Package pack = new Package(count.incrementAndGet(), DeliveryType.DTS);
            service.deliveryPackage(pack);

            Package pack1 = new Package(count.incrementAndGet(), DeliveryType.DTH);
            service.deliveryPackage(pack1);

            Package pack2 = new Package(count.incrementAndGet(), DeliveryType.TRANSFER);
            service.deliveryPackage(pack2);
        }

        System.out.println("Hit 'Enter' to terminate");
        System.in.read();
        ctx.close();
    }

    @MessagingGateway
    public interface DeliveryService {

        @Gateway(requestChannel = "packages.input")
        void deliveryPackage(Package pack);
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedDelay(1000).get();
    }

    @Bean
    public IntegrationFlow packages() {
        return f -> f
                .channel(c -> c.executor(Executors.newCachedThreadPool()))
                .<Package, DeliveryType>route(Package::getType, mapping -> mapping
                        .subFlowMapping(DeliveryType.DTH, sf -> sf
                                .channel(c -> c.queue(10))
                                .publishSubscribeChannel(c -> c
                                        .subscribe(s -> s.handle(m -> sleepUninterruptibly(5, TimeUnit.SECONDS)))
                                        .subscribe(sub -> sub
                                                .<Package, String>transform(p ->
                                                        "Package # " + p.getId() + " delivered to home.")
                                                .handle(m -> System.out.println(m.getPayload()))))
                                .bridge())

                        .subFlowMapping(DeliveryType.DTS, sf -> sf
                                .channel(c -> c.queue(10))
                                .publishSubscribeChannel(c -> c
                                        .subscribe(s -> s.handle(m -> sleepUninterruptibly(2, TimeUnit.SECONDS)))
                                        .subscribe(sub -> sub
                                                .<Package, String>transform(p ->
                                                        "Package # " + p.getId() + " delivered to store.")
                                                .handle(m -> System.out.println(m.getPayload()))))
                                .bridge())

                        .subFlowMapping(DeliveryType.TRANSFER, sf -> sf
                                .channel(c -> c.queue(10))
                                .publishSubscribeChannel(c -> c
                                        .subscribe(s -> s.handle(m -> sleepUninterruptibly(1, TimeUnit.SECONDS)))
                                        .subscribe(sub -> sub
                                                .<Package, String>transform(p ->
                                                        "Package # " + p.getId() + " is waiting for transfer to another store.")
                                                .handle(m -> System.out.println(m.getPayload()))))
                                .bridge()))

                .handle(m -> System.out.println());
    }

    private static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
        boolean interrupted = false;
        try {
            unit.sleep(sleepFor);
        } catch (InterruptedException e) {
            interrupted = true;
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
