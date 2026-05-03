package systemdesigns.study.Bitly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import systemdesigns.study.Bitly.entity.Link;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    boolean existsByShortCode(String shortCode);

}
