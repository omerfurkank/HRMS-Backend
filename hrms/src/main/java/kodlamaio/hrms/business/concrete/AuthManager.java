package kodlamaio.hrms.business.concrete;

import java.time.LocalDate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kodlamaio.hrms.business.abstracts.AuthService;
import kodlamaio.hrms.business.abstracts.CandidateService;
import kodlamaio.hrms.business.abstracts.EmployerService;
import kodlamaio.hrms.business.abstracts.UserService;
import kodlamaio.hrms.business.abstracts.VerifyCodeCandidateService;
import kodlamaio.hrms.business.abstracts.VerifyCodeEmployerService;
import kodlamaio.hrms.core.entities.abstracts.User;
import kodlamaio.hrms.core.utilities.adapters.CheckService;
import kodlamaio.hrms.core.utilities.business.BusinessRules;
import kodlamaio.hrms.core.utilities.results.ErrorResult;
import kodlamaio.hrms.core.utilities.results.Result;
import kodlamaio.hrms.core.utilities.results.SuccessResult;
import kodlamaio.hrms.core.utilities.verification.VerificationService;
import kodlamaio.hrms.entities.concrete.Candidate;
import kodlamaio.hrms.entities.concrete.Employer;
import kodlamaio.hrms.entities.concrete.VerifyCodeCandidate;
import kodlamaio.hrms.entities.concrete.VerifyCodeEmployer;
import kodlamaio.hrms.entities.dtos.RegisterCandidateDto;
import kodlamaio.hrms.entities.dtos.RegisterEmployerDto;
import kodlamaio.hrms.entities.dtos.VerifyCandidateDto;
import kodlamaio.hrms.entities.dtos.VerifyEmployerDto;

@Service
public class AuthManager implements AuthService {

	private UserService userService;
	private CandidateService candidateService;
	private EmployerService employerService;
	private VerificationService verificationService;
	private VerifyCodeCandidateService verifyCodeCandidateService;
	private VerifyCodeEmployerService verifyCodeEmployerService;
	private CheckService checkService;
	private ModelMapper modelMapper;
	
	@Autowired
	public AuthManager(UserService userService,CandidateService candidateService, EmployerService employerService,
			VerificationService verificationService, VerifyCodeCandidateService verifyCodeCandidateService,
			VerifyCodeEmployerService verifyCodeEmployerService, ModelMapper modelMapper, CheckService checkService) {
		super();
		this.userService=userService;
		this.candidateService = candidateService;
		this.employerService = employerService;
		this.verificationService = verificationService;
		this.verifyCodeCandidateService = verifyCodeCandidateService;
		this.verifyCodeEmployerService = verifyCodeEmployerService;
		this.checkService = checkService;
		this.modelMapper=modelMapper;
	}

	@Override
	public Result registerCandidate(RegisterCandidateDto registerCandidateDto) {

		Result result= BusinessRules.run(
				checkPasswordConfirm(registerCandidateDto.getPassword(), registerCandidateDto.getPasswordConfirm()),
				checkIfRealPerson(registerCandidateDto.getIdentityNumber(), registerCandidateDto.getFirstName(),registerCandidateDto.getLastName(), registerCandidateDto.getBirthDate()),
				checkIfCandidateExistsByIDNo(registerCandidateDto.getIdentityNumber()),
				checkIfUserExistsByEmail(registerCandidateDto.getEmail())
				);
		if (!result.isSuccess()) {
			return result;
		}
		
		Candidate registeredCandidate= modelMapper.map(registerCandidateDto, Candidate.class);
		String code = verificationService.generateEmailCode();	
		verificationService.sendEmailLink(registeredCandidate.getEmail());
		
		this.candidateService.add(registeredCandidate);
		this.verifyCodeCandidateService.add(new VerifyCodeCandidate(code,false,registeredCandidate.getId()));
		
		return new SuccessResult("Registration successful! Please check email to verify your account.");
	}

	@Override
	public Result registerEmployer(RegisterEmployerDto registerEmployerDto) {
		
		Result result = BusinessRules.run(
				checkPasswordConfirm(registerEmployerDto.getPassword(), registerEmployerDto.getPasswordConfirm()),
				checkEmailDomain(registerEmployerDto.getEmail(), registerEmployerDto.getWebAdress()),
				checkIfUserExistsByEmail(registerEmployerDto.getEmail())
				);
		if (!result.isSuccess()) {
			return result;
		}
		
		Employer registeredEmployer=modelMapper.map(registerEmployerDto, Employer.class);
		String code = verificationService.generateEmailCode();	
		verificationService.sendEmailLink(registeredEmployer.getEmail());
		
		this.employerService.add(registeredEmployer);
		this.verifyCodeEmployerService.add(new VerifyCodeEmployer(code, false, registeredEmployer.getId()));
		
		return new SuccessResult("Registration successful! Please check email to verify your account.");
	}
	
	@Override
	public Result verifyCandidate(VerifyCandidateDto verifyCandidateDto) {
		
		User verifiedCandidate = this.userService.getByEmail(verifyCandidateDto.getEmail()).getData();
		VerifyCodeCandidate verifiedCode =this.verifyCodeCandidateService.getByCandidateId(verifiedCandidate.getId()).getData();
		if (verifiedCode.getCode().equals(verifyCandidateDto.getCode())) {
			verifiedCode.setVerified(true);
			verifiedCode.setVerifiedDate(LocalDate.now());
			this.verifyCodeCandidateService.update(verifiedCode);
			return new SuccessResult("Account verified!");
		}
		return new ErrorResult("Codes are not the same!");
	}
	
	@Override
	public Result verifyEmployer(VerifyEmployerDto verifyEmployerDto) {
		
		User verifiedEmployer = this.userService.getByEmail(verifyEmployerDto.getEmail()).getData();
		VerifyCodeEmployer verifiedCode=this.verifyCodeEmployerService.getByEmployerId(verifiedEmployer.getId()).getData();
		if (verifiedCode.getCode().equals(verifyEmployerDto.getCode())) {
			verifiedCode.setVerified(true);
			verifiedCode.setVerifiedDate(LocalDate.now());
			this.verifyCodeEmployerService.update(verifiedCode);
			return new SuccessResult("Account verified!");
		}
		return new ErrorResult("Codes are not the same!");
	}
	
	
	private Result checkIfRealPerson(String tcno,String firsName,String lastName,LocalDate birthDate) {
		 return this.checkService.checkIfRealPerson(tcno, firsName, lastName, birthDate);
		
	}
	
	private Result checkIfUserExistsByEmail(String email) {
		if (this.userService.getByEmail(email).getData()==null) {
			return new SuccessResult();
		}
			return new ErrorResult("this user already exists");
	}
	
	private Result checkPasswordConfirm(String password,String passwordConfirm) {
		if (password.equals(passwordConfirm)) {
			return new SuccessResult();
		}
		return new ErrorResult("passwords do not match");
	}
	
	private Result checkIfCandidateExistsByIDNo(String identityNumber) {
		if(this.candidateService.getByIdentityNumber(identityNumber).getData()==null) {
			return new SuccessResult();
		}
		return new ErrorResult("this user already exists");
	}
	
	private Result checkEmailDomain(String email,String website) {
		String[] emailSplit = email.split("@");
		if (emailSplit[1].equals(website)) {
			return new SuccessResult();
		}
		return new ErrorResult("email do nat match website domain");
	}

	
	
	
	

}
