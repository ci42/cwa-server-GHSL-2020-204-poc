package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class DiagnosisKeyRepository implements PagingAndSortingRepository<DiagnosisKey, Long> {

  @Autowired
  public DiagnosisKeyRepositoryDelegate repo;

  @Autowired
  public JdbcTemplate jdbcTemplate;

  @Override
  public Iterable<DiagnosisKey> findAll(Sort sort) {
    return repo.findAll(sort);
  }

  @Override
  public Page<DiagnosisKey> findAll(Pageable pageable) {
    return repo.findAll(pageable);
  }

  @Override
  public <S extends DiagnosisKey> S save(S entity) {
    return repo.save(entity);
  }

  @Override
  public <S extends DiagnosisKey> Iterable<S> saveAll(Iterable<S> entities) {
    return repo.saveAll(entities);
  }

  @Override
  public Optional<DiagnosisKey> findById(Long aLong) {
    return repo.findById(aLong);
  }

  @Override
  public boolean existsById(Long aLong) {
    return repo.existsById(aLong);
  }

  @Override
  public Iterable<DiagnosisKey> findAll() {
    return repo.findAll();
  }

  @Override
  public Iterable<DiagnosisKey> findAllById(Iterable<Long> longs) {
    return repo.findAllById(longs);
  }

  @Override
  public long count() {
    return repo.count();
  }

  @Override
  public void deleteById(Long aLong) {
    repo.deleteById(aLong);
  }

  @Override
  public void delete(DiagnosisKey entity) {
    repo.delete(entity);
  }

//  @Override
//  public void deleteAllById(Iterable<? extends Long> longs) {
//    repo.deleteAllById(longs);
//  }

  @Override
  public void deleteAll(Iterable<? extends DiagnosisKey> entities) {
    repo.deleteAll(entities);
  }

  @Override
  public void deleteAll() {
    repo.deleteAll();
  }

  public boolean exists(@Param("key_data") byte[] keyData, @Param("submission_type") String submissionType) {
    String sql = "SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) "
        + "FROM diagnosis_key "
        + "WHERE key_data="+ byteArrayToHexString(keyData) + " "
        + "AND submission_type='" + submissionType + "'";
    return jdbcTemplate.queryForObject(sql, Boolean.class, keyData, submissionType);

  }

  public int countOlderThan(@Param("threshold") long submissionTimestamp) {
    String sql = "SELECT COUNT(*) FROM diagnosis_key WHERE submission_timestamp<"+ submissionTimestamp + "";
    return jdbcTemplate.queryForObject(sql, Integer.class, submissionTimestamp);
  }

  public void deleteOlderThan(@Param("threshold") long submissionTimestamp) {
    String sql = "DELETE FROM diagnosis_key WHERE submission_timestamp<"+ submissionTimestamp + "";
    jdbcTemplate.execute(sql);
  }

  public List<DiagnosisKey> findAllWithTrlGreaterThanOrEqual(@Param("minTrl") int minTrl, @Param("threshold")  long submissionTimestamp) {
    String sql = "SELECT * FROM diagnosis_key WHERE transmission_risk_level>=" + minTrl + " AND submission_timestamp>=" + submissionTimestamp
        + " ORDER BY submission_timestamp)";
    return jdbcTemplate.queryForList(sql, DiagnosisKey.class, minTrl, submissionTimestamp);
  }

  public boolean saveDoNothingOnConflict(
      @Param("keyData") byte[] keyData,
      @Param("rollingStartIntervalNumber") int rollingStartIntervalNumber,
      @Param("rollingPeriod") int rollingPeriod,
      @Param("submissionTimestamp") long submissionTimestamp,
      @Param("transmissionRisk") int transmissionRisk,
      @Param("origin_country") String originCountry,
      @Param("visited_countries") String[] visitedCountries,
      @Param("report_type") String reportType,
      @Param("days_since_onset_of_symptoms") int daysSinceOnsetOfSymptoms,
      @Param("consent_to_federation") boolean consentToFederation) {
    String sql = "INSERT INTO diagnosis_key "
        + "(key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, "
        + "origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation) "
        + "VALUES ('" + byteArrayToHexString(keyData) + "', " + rollingStartIntervalNumber + ", "
        + rollingPeriod + ", " + submissionTimestamp + ", " + transmissionRisk + ", "
        + originCountry + ", " + visitedCountries + ", " + reportType + ", "
        + daysSinceOnsetOfSymptoms + ", " + consentToFederation + ") "
        + "ON CONFLICT DO NOTHING";
    return jdbcTemplate.queryForObject(sql, Boolean.class);
  }

  private static String byteArrayToHexString(byte[] bytes){
    StringBuilder hexString = new StringBuilder();
    hexString.append("E\\\\x");
    for (byte b : bytes) {
      hexString.append(String.format("%02x", b));
    }
    hexString.append("::bytea ");
    return hexString.toString();
  }
}

