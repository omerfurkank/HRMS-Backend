package kodlamaio.hrms.business.abstracts;

import java.util.List;

import kodlamaio.hrms.core.utilities.results.DataResult;
import kodlamaio.hrms.core.utilities.results.Result;
import kodlamaio.hrms.entities.concrete.Candidate;
import kodlamaio.hrms.entities.dtos.CandidateCvDto;

public interface CandidateService {

	Result add(Candidate candidate);
	
	DataResult<List<Candidate>> getAll();
	
	DataResult<Candidate> getById(int id);
	
	DataResult<Candidate> getByIdentityNumber(String identityNumber);
	
	DataResult<CandidateCvDto> getCvById(int id);
}
