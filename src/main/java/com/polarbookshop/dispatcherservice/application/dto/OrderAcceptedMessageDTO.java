package com.polarbookshop.dispatcherservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OrderAcceptedMessageDTO {

    Long orderId;
}
