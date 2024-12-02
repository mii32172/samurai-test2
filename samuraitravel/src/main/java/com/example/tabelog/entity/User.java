package com.example.tabelog.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="users")
@Data
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="furigana")
	private String furigana;

	@Column(name="email")
	private String email;

	@Column(name="password")
	private String password;
	
	@ManyToOne
	@JoinColumn(name="role_id")
	private Role role;
	
	@Column(name="enabled")
	private Boolean enabled;
	
	@Column(name="created_at", insertable=false, updatable=false)
	private Timestamp createdAt;
	
	@Column(name="updated_at", insertable=false, updatable=false)
	private Timestamp updatedAt;
	
	@Column(name = "customer_id", nullable = true)
	private String customerId;

	@Column(name = "subscription_id", nullable = true)
	private String subscriptionId;

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public int getId() {
		return this.id;
	}

}
