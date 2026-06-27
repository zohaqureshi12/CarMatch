package com.carmatch.entity;

import com.carmatch.enums.SwipeDirection;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "swipes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "car_id"}))
public class Swipe extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SwipeDirection direction;

    private LocalDateTime swipedAt;

    public Swipe() {}

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    public SwipeDirection getDirection() { return direction; }
    public void setDirection(SwipeDirection direction) { this.direction = direction; }

    public LocalDateTime getSwipedAt() { return swipedAt; }
    public void setSwipedAt(LocalDateTime swipedAt) { this.swipedAt = swipedAt; }
}