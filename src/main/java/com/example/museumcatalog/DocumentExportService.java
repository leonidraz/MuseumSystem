package com.example.museumcatalog;

import com.example.museumcatalog.Models.*;
import com.example.museumcatalog.Storages.DocumentRelationsRepository;
import com.example.museumcatalog.Storages.DocumentTypeDetailsRepository;
import com.example.museumcatalog.Storages.OwnerRepository;

import java.sql.SQLException;
import java.util.List;

public class DocumentExportService {
    public static FullDocumentData load(Document document) throws SQLException {

        FullDocumentData data = new FullDocumentData();

        data.setDocument(document);

        if (document.getOwnerId() != null) {
            Owner owner = OwnerRepository.getOwners()
                    .stream()
                    .filter(o -> o.getId() == document.getOwnerId())
                    .findFirst()
                    .orElse(null);

            data.setOwner(owner);
        }

        List<DocumentEmployeeRelation> employees = DocumentRelationsRepository.getEmployees(document.getId());

        List<Exhibit> exhibits = DocumentRelationsRepository.getExhibits(document.getId());

        data.setEmployees(employees);
        data.setExhibits(exhibits);

        String type = document.getDocType();

        switch (type) {
            case "Протокол заседания ЭФЗК" -> {
                List<EfzkData> efzk = DocumentTypeDetailsRepository.getEfzk(document.getId());
                if (!efzk.isEmpty()) {
                    data.setEfzkData(efzk.getFirst());
                }
            }
            case "Акт внутримузейной передачи" -> {
                List<InternalTransferData> transfer = DocumentTypeDetailsRepository.getInternalTransfer(document.getId());
                if (!transfer.isEmpty()) {
                    data.setInternalTransferData(transfer.getFirst());
                }
            }
            case "Акт ВП на временное хранение" -> {
                List<TemporaryStorageData> storage = DocumentTypeDetailsRepository.getTemporaryStorage(document.getId());
                if (!storage.isEmpty()) {
                    data.setTemporaryStorageData(storage.getFirst());
                }
            }
        }

        return data;
    }

}