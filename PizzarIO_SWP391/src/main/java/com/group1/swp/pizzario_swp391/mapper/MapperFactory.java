package com.group1.swp.pizzario_swp391.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory class to provide access to all mappers
 * This follows the Factory pattern and provides a centralized way to access mappers
 */
@Component
public class MapperFactory {
    
    @Autowired
    private VoucherMapper voucherMapper;
    
    @Autowired
    private VoucherMapperAdvanced voucherMapperAdvanced;
    
    /**
     * Get the standard VoucherMapper
     * @return VoucherMapper instance
     */
    public VoucherMapper getVoucherMapper() {
        return voucherMapper;
    }
    
    /**
     * Get the advanced VoucherMapper
     * @return VoucherMapperAdvanced instance
     */
    public VoucherMapperAdvanced getVoucherMapperAdvanced() {
        return voucherMapperAdvanced;
    }
    
    /**
     * Get the standard VoucherMapper as interface
     * @return VoucherMapperInterface instance
     */
    public VoucherMapperInterface getVoucherMapperInterface() {
        return voucherMapper;
    }
}
