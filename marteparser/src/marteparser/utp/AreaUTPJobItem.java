package marteparser.utp;

import java.util.Collection;
import java.util.Map;

public class AreaUTPJobItem {
	private final String jobId;
	private final String jobName;
	private final String jobItemId;
	private final String jobItemType;
	private final Map<String, String> jobInfo;
	private final Map<String, String> jobItemInfo;
	private final Collection<String> jobSMUs;
	private final Collection<String> jobItemSMUs;

	public AreaUTPJobItem(String jobId, String jobName, String jobItemId, String jobItemType, Map<String, String> jobInfo, Map<String, String> jobItemInfo, Collection<String> jobSMUs, Collection<String> jobItemSMUs){
		this.jobId = jobId;
		this.jobName = jobName;
		this.jobItemId = jobItemId;
		this.jobItemType = jobItemType;
		this.jobInfo = jobInfo;
		this.jobItemInfo = jobItemInfo;
		this.jobSMUs = jobSMUs;
		this.jobItemSMUs = jobItemSMUs;
	}

	public String getJobId() {
		return jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public String getJobItemId() {
		return jobItemId;
	}

	public String getJobItemType() {
		return jobItemType;
	}

	public Map<String, String> getJobInfo() {
		return jobInfo;
	}

	public Map<String, String> getJobItemInfo() {
		return jobItemInfo;
	}

	public Collection<String> getJobSMUs() {
		return jobSMUs;
	}

	public Collection<String> getJobItemSMUs() {
		return jobItemSMUs;
	}
	
}
