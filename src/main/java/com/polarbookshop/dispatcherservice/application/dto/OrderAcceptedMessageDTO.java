package com.polarbookshop.dispatcherservice.application.dto;

import lombok.Data;

/**
 * This class represents the DTO containing
 * the order identifier.
 */
@Data
public class OrderAcceptedMessageDTO {

    Long orderId;
}
