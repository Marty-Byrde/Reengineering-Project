package org.billthefarmer.editor.dto;

public class FileSizeDTO {
    private boolean toLarge;
    private long size;

    public FileSizeDTO(boolean toLarge,long size) {
        this.toLarge = toLarge;
        this.size = size;
    }

    public boolean isToLarge() {
        return toLarge;
    }

    public long getSize() {
        return size;
    }
}
