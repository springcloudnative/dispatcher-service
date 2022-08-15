package com.polarbookshop.dispatcherservice.domain.events;

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
public class OrderDispatchedEvent {

    Long orderId;
}
