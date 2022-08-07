package com.arsenik.minioservice.domain;


import com.arsenik.minioservice.domain.enumeration.DocType;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A FileInfo.
 */
@Entity
@Table(name = "file_info")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "base_file_name", nullable = false, unique = true)
    private String baseFileName;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long size;

    @Column(name = "user_id")
    private String userId;

    @NotNull
    @Column(name = "user_can_write", nullable = false)
    private Boolean userCanWrite;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private DocType docType;

    @OneToOne
    @JoinColumn(unique = true)
    private UserFileInfo userFileInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaseFileName() {
        return baseFileName;
    }

    public FileInfo baseFileName(String baseFileName) {
        this.baseFileName = baseFileName;
        return this;
    }

    public void setBaseFileName(String baseFileName) {
        this.baseFileName = baseFileName;
    }

    public Long getSize() {
        return size;
    }

    public FileInfo size(Long size) {
        this.size = size;
        return this;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUserId() {
        return userId;
    }

    public FileInfo userId(String userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean isUserCanWrite() {
        return userCanWrite;
    }

    public FileInfo userCanWrite(Boolean userCanWrite) {
        this.userCanWrite = userCanWrite;
        return this;
    }

    public void setUserCanWrite(Boolean userCanWrite) {
        this.userCanWrite = userCanWrite;
    }

    public DocType getDocType() {
        return docType;
    }

    public FileInfo docType(DocType docType) {
        this.docType = docType;
        return this;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public UserFileInfo getUserFileInfoInfo() {
        return userFileInfo;
    }

    public FileInfo UserFileInfo(UserFileInfo userFileInfo) {
        this.userFileInfo = userFileInfo;
        return this;
    }

    public void setUserFileInfo(UserFileInfo userFileInfo) {
        this.userFileInfo = userFileInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        if (fileInfo.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), fileInfo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "FileInfo{" +
            "id=" + getId() +
            ", baseFileName='" + getBaseFileName() + "'" +
            ", size=" + getSize() +
            ", userId='" + getUserId() + "'" +
            ", userCanWrite='" + isUserCanWrite() + "'" +
            ", docType='" + getDocType() + "'" +
            "}";
    }
}
