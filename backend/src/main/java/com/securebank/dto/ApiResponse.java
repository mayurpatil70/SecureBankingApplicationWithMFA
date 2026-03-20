package com.securebank.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}

/* 
```

---

## ✅ **BATCH 1.3 DONE!**

**Save all files** (`Ctrl+S` on each).

---

## 📊 **Progress Update:**
```
✅ BATCH 1.1: Models (3 files) - DONE
✅ BATCH 1.2: Repositories (3 files) - DONE
✅ BATCH 1.3: DTOs (6 files) - DONE

Total: 12 files created!

Next: Security & JWT configuration */