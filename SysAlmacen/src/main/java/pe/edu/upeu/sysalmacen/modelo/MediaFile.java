package pe.edu.upeu.sysalmacen.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "upeu_media_file")
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFile;
    @Column(length = 50, nullable = false)
    private String fileName;
    @Column(length =30, nullable = false)
    private String fileType;
    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] content;
}
