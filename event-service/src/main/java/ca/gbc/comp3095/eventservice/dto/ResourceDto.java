package ca.gbc.comp3095.eventservice.dto;

public class ResourceDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String url;
    
    // Legacy fields for backward compatibility
    private String name;
    private String type;
    private String info;

    public ResourceDto() {}
    
    public ResourceDto(Long id, String title, String description, String category, String url) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.url = url;
        // Map to legacy fields
        this.name = title;
        this.type = category;
        this.info = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title;
        this.name = title; // Keep in sync
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.info = description; // Keep in sync
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.type = category; // Keep in sync
    }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    // Legacy getters/setters
    public String getName() { return name != null ? name : title; }
    public void setName(String name) { 
        this.name = name;
        this.title = name; // Keep in sync
    }
    
    public String getType() { return type != null ? type : category; }
    public void setType(String type) { 
        this.type = type;
        this.category = type; // Keep in sync
    }
    
    public String getInfo() { return info != null ? info : description; }
    public void setInfo(String info) { 
        this.info = info;
        this.description = info; // Keep in sync
    }
}
