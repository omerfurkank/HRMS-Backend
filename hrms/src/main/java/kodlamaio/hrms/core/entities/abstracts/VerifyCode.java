package kodlamaio.hrms.core.entities.abstracts;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "verification_codes")
@Inheritance(strategy = InheritanceType.JOINED)
public class VerifyCode {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@NotNull
	@NotBlank
	@Column(name = "code")
	private String code;
	
	@NotNull
	@NotBlank
	@Column(name = "is_verified")
	private boolean isVerified;
	
	@Column(name = "verified_date")
	private LocalDate verifiedDate;

	public VerifyCode(@NotNull @NotBlank String code, @NotNull @NotBlank boolean isVerified) {
		super();
		this.code = code;
		this.isVerified = isVerified;
	}
	
}
