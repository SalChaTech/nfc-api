package com.salcatech.nfc_api.exception;

import com.salcatech.nfc_api.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of(
                        "message", "Dosya boyutu çok büyük! Maksimum 20MB yükleyebilirsin.",
                        "status", "error"
                ));
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidJwt(InvalidJwtException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(FileSizeExceedException.class)
    public ResponseEntity<ApiResponse<String>> fileSizeExceed(FileSizeExceedException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                "Validation failed",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DeleteFileException.class)
    public ResponseEntity<ApiResponse<String>> deleteFileException(DeleteFileException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(FolderNotGetOrCreateException.class)
    public ResponseEntity<ApiResponse<String>> folderNotGetOrCreateException(FolderNotGetOrCreateException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiResponse<String>> forbiddenOperationException(ForbiddenOperationException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ListFilesException.class)
    public ResponseEntity<ApiResponse<String>> listFilesException(ListFilesException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NotFetchedAccessTokenFromCode.class)
    public ResponseEntity<ApiResponse<String>> notFetchedAccessTokenFromCode(NotFetchedAccessTokenFromCode ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(NotFetchedUserInfoFromAccessCode.class)
    public ResponseEntity<ApiResponse<String>> notFetchedUserInfoFromAccessCode(NotFetchedUserInfoFromAccessCode ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(NotMatchedFilesException.class)
    public ResponseEntity<ApiResponse<String>> notMatchedFilesException(NotMatchedFilesException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PlaceToVisitDataNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> placeToVisitDataNotFoundException(PlaceToVisitDataNotFoundException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UploadFileException.class)
    public ResponseEntity<ApiResponse<String>> uploadFileException(UploadFileException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserProductNotClaimedException.class)
    public ResponseEntity<ApiResponse<String>> userProductNotClaimedException(UserProductNotClaimedException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserProductNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> userProductNotFoundException(UserProductNotFoundException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserProductOwnershipException.class)
    public ResponseEntity<ApiResponse<String>> userProductOwnershipException(UserProductOwnershipException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


    @ExceptionHandler(WeddingMemoryDataNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> weddingMemoryDataNotFoundException(WeddingMemoryDataNotFoundException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

}
