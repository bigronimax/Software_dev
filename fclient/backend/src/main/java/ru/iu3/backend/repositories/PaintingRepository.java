package ru.iu3.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iu3.backend.models.Painting;
import ru.iu3.backend.models.User;

@Repository
public interface PaintingRepository extends JpaRepository<Painting, Long>
{

}
