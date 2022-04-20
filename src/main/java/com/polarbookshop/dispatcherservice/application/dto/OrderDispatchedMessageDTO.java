package com.polarbookshop.dispatcherservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents the DTO containing
 * the order identifier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDispatchedMessageDTO {

    Long orderId;
}
