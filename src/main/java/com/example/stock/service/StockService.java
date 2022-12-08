package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    //@Transactional
    //synchronized을 사용하는게 실패한 이유는 @Transactional의 동작 방식을 이해해야한다.
    // 재고를 감소하고 트랜잭션을 종료를 할 그순간에 다른 쓰레드가 값을 업데이트를 하여 순서가 꼬이게된다.
    public synchronized void decrease(Long id,Long quantity) {
        // 재고를 가져온다.
        // 재고 감소
        // 저장
        Stock stock=stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }
}
