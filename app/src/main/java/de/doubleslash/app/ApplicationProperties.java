package de.doubleslash.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApplicationProperties {

   private String buildVersion;
   private String buildTimestamp;

   private String h2Version;

   private String springDataSourceUrl;
   private String springDataSourceUserName;
   private String springDataSourcePassword;

   private String gitCommitId;
   private String gitCommitTime;
   private String gitBranch;
   private String gitDirty;

   public String getBuildVersion() {
      return buildVersion;
   }

   @Value("${build.version}")
   private void setBuildVersion(String projectVersion) {
      this.buildVersion = projectVersion;
   }

   public String getBuildTimestamp() {
      return buildTimestamp;
   }

   @Value("${build.timestamp}")
   public void setBuildTimestamp(String buildTimestamp) {
      this.buildTimestamp = buildTimestamp;
   }

   public String getH2Version() {
      return this.h2Version;
   }

   @Value("${h2.version}")
   private void setH2Version(final String h2Version) {
      this.h2Version = h2Version;
   }

   public String getSpringDataSourceUrl() {
      return springDataSourceUrl;
   }

   @Value("${spring.datasource.url}")
   private void setSpringDataSourceUrl(String springDataSourceUrl) {
      this.springDataSourceUrl = springDataSourceUrl;
   }

   public String getSpringDataSourceUserName() {
      return springDataSourceUserName;
   }

   @Value("${spring.datasource.username}")
   private void setSpringDataSourceUserName(String springDataSourceUserName) {
      this.springDataSourceUserName = springDataSourceUserName;
   }

   public String getSpringDataSourcePassword() {
      return springDataSourcePassword;
   }

   @Value("${spring.datasource.password}")
   private void setSpringDataSourcePassword(String springDataSourcePassword) {
      this.springDataSourcePassword = springDataSourcePassword;
   }

   public String getGitCommitId() {
      return gitCommitId;
   }

   @Value("${git.commit.id}")
   private void setGitCommitId(String gitCommitId) {
      this.gitCommitId = gitCommitId;
   }

   public String getGitCommitTime() {
      return gitCommitTime;
   }

   @Value("${git.commit.time}")
   private void setGitCommitTime(String gitCommitTime) {
      this.gitCommitTime = gitCommitTime;
   }

   public String getGitBranch() {
      return gitBranch;
   }

   @Value("${git.branch}")
   private void setGitBranch(String gitBranch) {
      this.gitBranch = gitBranch;
   }

   public String getGitDirty() {
      return gitDirty;
   }

   @Value("${git.dirty}")
   private void setGitDirty(String gitDirty) {
      this.gitDirty = gitDirty;
   }
}
