package com.polarbookshop.dispatcherservice.domain.events;

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
public class OrderAcceptedEvent {

    Long orderId;
}
