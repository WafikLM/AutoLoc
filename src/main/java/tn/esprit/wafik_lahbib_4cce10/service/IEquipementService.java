package tn.esprit.wafik_lahbib_4cce10.service;

import java.util.List;
import tn.esprit.wafik_lahbib_4cce10.domain.Equipement;

public interface IEquipementService {

    Equipement create(Equipement equipement);

    Equipement findById(Long id);

    List<Equipement> findAll();

    Equipement update(Long id, Equipement equipement);

    void deleteById(Long id);
}
