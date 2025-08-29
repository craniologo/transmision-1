package pe.gob.onpe.wsonpe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TAB_CONF_TX")
@Data
@NoArgsConstructor
public class TabConfTx {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "N_CONF_TX_PK")
    private Integer idConfTx;

    @Column(length = 20, name = "C_DESCRIPCION", nullable = false)
    private String cDescripcion;

    @Column(length = 150, name = "C_ENC_SEA")
    private String cEncSea;

    @Column(length = 150, name = "C_ENC_FILE")
    private String cEncFile;

    @Column(length = 150, name = "C_ENC_VEP")
    private String cEncVep;

    @Column(length = 150, name = "C_ENC_FIRMA")
    private String cEncFirma;

    @Column(length = 150, name = "C_ENC_QR")
    private String cEncQr;

    @Column(length = 150, name = "C_MASTER_KEY")
    private String cMasterKey;

    @Column(name = "N_TX_MESA")
    private Integer nTxMesa;

    @Column(name = "N_ESTADO")
    private Integer nEstado;

    @Column(length = 150, name = "C_AES_SEA")
    private String cAesSea;

    @Column(length = 300, name = "C_TRUST_STORE_PATH")
    private String cTrustStorePath;

    @Column(length = 300, name = "C_ONPE_DEVICES_CA_CRL")
    private String cOnpeDevicesCaCrl;

    @Column(length = 300, name = "C_ONPE_ROOT_CA_CRL")
    private String cOnpeRootCaCrl;

    @Column(length = 300, name = "C_TRUST_STORE_PWD")
    private String cTrustStorePwd;

    @Column(length = 300, name = "C_HSQL_USR")
    private String chsqlUsr;

    @Column(length = 300, name = "C_HSQL_PWD")
    private String chsqlPwd;

}
