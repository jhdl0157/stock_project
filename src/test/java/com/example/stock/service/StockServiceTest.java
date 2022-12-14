package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.facade.NamedLockStockFacade;
import com.example.stock.facade.OptimisticLockStockFacade;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private PessimisticLockStockService pessimisticLockStockService;

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    @Autowired
    private NamedLockStockFacade namedLockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before(){
        var stock=new Stock(1L,100L);
        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void stack_decrease(){
        stockService.decrease(1L,1L);

        //100 -1 = 99
        var stock=stockRepository.findById(1L).orElseThrow();

        assertEquals(99,stock.getQuantity());
    }

    @Test
    public void 동시에_100개의_요청() throws InterruptedException {
        int threadCount=100;
        ExecutorService executorService= Executors.newFixedThreadPool(32);
        CountDownLatch latch=new CountDownLatch(threadCount);
        for(int i=0;i<threadCount;i++){
            //경쟁 상태가 들어가서 의도한 바와 다른 결과가 나온다.
            executorService.submit(()->{
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        var stock=stockRepository.findById(1L).orElseThrow();

        //100 -100 =0
        assertEquals(0L,stock.getQuantity());
    }

    @Test
    public void 비관적락_동시에_100개의_요청() throws InterruptedException {
        int threadCount=100;
        ExecutorService executorService= Executors.newFixedThreadPool(32);
        CountDownLatch latch=new CountDownLatch(threadCount);
        for(int i=0;i<threadCount;i++){
            executorService.submit(()->{
                try {
                    pessimisticLockStockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        var stock=stockRepository.findById(1L).orElseThrow();

        //100 -100 =0
        assertEquals(0L,stock.getQuantity());
    }

    @Test
    public void 낙관적락_동시에_100개의_요청() throws InterruptedException {
        int threadCount=100;
        ExecutorService executorService= Executors.newFixedThreadPool(32);
        CountDownLatch latch=new CountDownLatch(threadCount);
        for(int i=0;i<threadCount;i++){
            executorService.submit(()->{
                try {
                    optimisticLockStockFacade.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        var stock=stockRepository.findById(1L).orElseThrow();

        //100 -100 =0
        assertEquals(0L,stock.getQuantity());
    }

    @Test
    public void NamedLock_동시에_100개의_요청() throws InterruptedException {
        int threadCount=100;
        ExecutorService executorService= Executors.newFixedThreadPool(32);
        CountDownLatch latch=new CountDownLatch(threadCount);
        for(int i=0;i<threadCount;i++){
            executorService.submit(()->{
                try {
                    namedLockStockFacade.decrease(1L, 1L);
                    System.out.println("감소 +1");
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        var stock=stockRepository.findById(1L).orElseThrow();

        //100 -100 =0
        assertEquals(0L,stock.getQuantity());
    }


}