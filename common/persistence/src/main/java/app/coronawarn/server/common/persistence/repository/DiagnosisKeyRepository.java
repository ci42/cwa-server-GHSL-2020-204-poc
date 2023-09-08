

package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisKeyRepository extends PagingAndSortingRepository<DiagnosisKey, Long> {

  /**
   * Counts all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be expired.
   * @return The number of expired keys.
   */
  @Query("SELECT COUNT(*) FROM diagnosis_key WHERE submission_timestamp<:threshold")
  int countOlderThan(@Param("threshold") long submissionTimestamp);

  /**
   * Deletes all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be deleted.
   */
  @Modifying
  @Query("DELETE FROM diagnosis_key WHERE submission_timestamp<:threshold")
  void deleteOlderThan(@Param("threshold") long submissionTimestamp);


  /**
   * Attempts to write the specified diagnosis key information into the database. If a row with the specified key data
   * already exists, no data is inserted.
   *
   * @param keyData                    The key data of the diagnosis key.
   * @param rollingStartIntervalNumber The rolling start interval number of the diagnosis key.
   * @param rollingPeriod              The rolling period of the diagnosis key.
   * @param submissionTimestamp        The submission timestamp of the diagnosis key.
   * @param transmissionRisk           The transmission risk level of the diagnosis key.
   * @param originCountry              The origin country from the app.
   * @param visitedCountries           The list of countries this transmissions is relevant for.
   * @param reportType                 The report type of the diagnosis key.
   * @return {@literal true} if the diagnosis key was inserted successfully, {@literal false} otherwise.
   */
  @Modifying
  @Query("INSERT INTO diagnosis_key "
      + "(key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, "
      + "origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation) "
      + "VALUES (:keyData, :rollingStartIntervalNumber, :rollingPeriod, :submissionTimestamp, :transmissionRisk, "
      + ":origin_country, :visited_countries, :report_type, :days_since_onset_of_symptoms, :consent_to_federation) "
      + "ON CONFLICT DO NOTHING")
  boolean saveDoNothingOnConflict(
      @Param("keyData") byte[] keyData,
      @Param("rollingStartIntervalNumber") int rollingStartIntervalNumber,
      @Param("rollingPeriod") int rollingPeriod,
      @Param("submissionTimestamp") long submissionTimestamp,
      @Param("transmissionRisk") int transmissionRisk,
      @Param("origin_country") String originCountry,
      @Param("visited_countries") String[] visitedCountries,
      @Param("report_type") String reportType,
      @Param("days_since_onset_of_symptoms") int daysSinceOnsetOfSymptoms,
      @Param("consent_to_federation") boolean consentToFederation);


  /**
   * Attempts to write the specified diagnosis key information into the database. If a row with the specified key data
   * already exists, no data is inserted.
   *
   * @param keyData                    The key data of the diagnosis key.
   * @param rollingStartIntervalNumber The rolling start interval number of the diagnosis key.
   * @param rollingPeriod              The rolling period of the diagnosis key.
   * @param submissionTimestamp        The submission timestamp of the diagnosis key.
   * @param transmissionRisk           The transmission risk level of the diagnosis key.
   * @param originCountry              The origin country from the app.
   * @param visitedCountries           The list of countries this transmissions is relevant for.
   * @param reportType                 The report type of the diagnosis key.
   * @param jdbcTemplate               JdbcTemplate
   * @return {@literal true} if the diagnosis key was inserted successfully, {@literal false} otherwise.
   */
  default boolean saveDoNothingOnConflictEvil(
      byte[] keyData,
      int rollingStartIntervalNumber,
      int rollingPeriod,
      long submissionTimestamp,
      int transmissionRisk,
      String originCountry,
      String[] visitedCountries,
      String reportType,
      int daysSinceOnsetOfSymptoms,
      boolean consentToFederation,
      JdbcTemplate jdbcTemplate) {

    String sql = "INSERT INTO diagnosis_key "
        + "(key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, "
        + "origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation) "
        + "VALUES ('" + byteArrayToHexString(keyData) + "', " + rollingStartIntervalNumber + ", "
        + rollingPeriod + ", " + submissionTimestamp + ", " + transmissionRisk + ", "
        + originCountry + ", " + visitedCountries + ", " + reportType + ", "
        + daysSinceOnsetOfSymptoms + ", " + consentToFederation + ") "
        + "ON CONFLICT DO NOTHING";

    //    sql = "INSERT INTO diagnosis_key
    //    (key_data, rolling_period, rolling_start_interval_number, submission_timestamp,"
    //        + " transmission_risk_level, origin_country) VALUES ('" + byteArrayToHexString(keyData)
    //        + "::bytea ', 0,0,0,0, " + originCountry + ");";
    System.out.println("sql = " + sql);
    return jdbcTemplate.queryForObject(sql, Boolean.class);
  }

  /**
   * Converts byte array into hexstring.
   * @param bytes the bytearray.
   * @return the hexstring.
   */
  private static String byteArrayToHexString(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    hexString.append("E\\\\x");
    for (byte b : bytes) {
      hexString.append(String.format("%02x", b));
    }
    hexString.append("::bytea ");
    return hexString.toString();
  }
}
