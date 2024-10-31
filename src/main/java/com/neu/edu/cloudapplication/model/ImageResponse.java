package com.neu.edu.cloudapplication.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ImageResponse {
    private String file_name;
    private UUID id;
    private String url;
    private Date upload_date;
    private UUID user_id;
}
