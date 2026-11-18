package com.allocator.model;

import java.util.Objects;

/**
 * Represents a physical Resource (Classroom, Laboratory, or Auditorium)
 * that is allocated to a Task during a specific colored Time Slot.
 */
public class Resource {
    public enum ResourceType {
        CLASSROOM,
        LAB,
        AUDITORIUM
    }

    private int id;
    private String code;
    private String name;
    private ResourceType type;
    private int capacity;
    private String building;
    private boolean available;

    public Resource() {
    }

    public Resource(int id, String code, String name, ResourceType type, int capacity, String building, boolean available) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.building = building;
        this.available = available;
    }

    public Resource(String code, String name, ResourceType type, int capacity, String building, boolean available) {
        this(0, code, name, type, capacity, building, available);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return code + " - " + name + " [" + type + ", Cap: " + capacity + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        Resource resource = (Resource) o;
        return id == resource.id && Objects.equals(code, resource.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }
}
