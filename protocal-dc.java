[
	{
		"id": 1027,
		"type": "element",
		"element": 100298,
		"name": "VSPDate",
		"displayName": "VSPDate",
		"absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/VSPDate",
		"relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/VSPDate",
		"logicalName": "VSPDate",
		"physicalName": "VSPDate",
		"relativePaths": [
			"ISG/CHWK/dictionary/mo-trade-message/current/$.body/VSPDate"
		],
		"tagAssociated": [],
		"cdeNames": [],
		"definition": "VSPDate",
		"dataType": "ExternalDataTypeReleaseReference",
		"nullable": true,
		"keys": {
			"foreign": false,
			"unique": false,
			"primary": false,
			"non-unique": false
		},
		"dataTypeDefinition": {
			"typeName": "ExternalDataTypeReleaseReference",
			"business": "ISG",
			"provider": "CHWK",
			"dictionary": "fix-fields",
			"releaseFlag": false,
			"dataType": "$defs.VSPDate",
			"type": "DATA_TYPE_REF",
			"absolutePath": "ISG/CHWK/dictionary/fix-fields/_v1",
			"releaseVersion": "v1"
		}
	},
	{
		"id": 183,
		"type": "group",
		"element": " ",
		"name": "AccountGrp",
		"displayName": "AccountGrp",
		"absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountGrp",
		"relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountGrp",
		"logicalName": "AccountGrp",
		"physicalName": "AccountGrp",
		"relativePaths": [
			"ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountGrp"
		],
		"tagAssociated": [],
		"cdeNames": [],
		"definition": "AccountGrp",
		"dataType": "ConceptReleaseReference",
		"nullable": true,
		"keys": {
			"foreign": false,
			"unique": false,
			"primary": false,
			"non-unique": false
		},
		"dataTypeDefinition": {
			"typeName": "ConceptReleaseReference",
			"business": "ISG",
			"provider": "CHWK",
			"dictionary": "account-grp",
			"releaseFlag": false,
			"concept": "$",
			"type": "CONCEPT_REF",
			"absolutePath": "ISG/CHWK/dictionary/account-grp/_v1/$",
			"releaseVersion": "v1"
		},
		"childAttributes": [
			{
				"id": 2491,
				"type": "element",
				"element": 10193,
				"name": "AccountCrossType",
				"displayName": "AccountCrossType",
				"absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountCrossType",
				"relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountCrossType",
				"logicalName": "AccountCrossType",
				"physicalName": "AccountCrossType",
				"relativePaths": [
					"ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountCrossType"
				],
				"tagAssociated": [],
				"cdeNames": [],
				"definition": "AccountCrossType",
				"dataType": "ExternalDataTypeReleaseReference",
				"nullable": true,
				"keys": {
					"foreign": false,
					"unique": false,
					"primary": false,
					"non-unique": false
				},
				"dataTypeDefinition": {
					"typeName": "ExternalDataTypeReleaseReference",
					"business": "ISG",
					"provider": "CHWK",
					"dictionary": "fix-fields",
					"releaseFlag": false,
					"dataType": "$defs.AccountCrossType",
					"type": "DATA_TYPE_REF",
					"absolutePath": "ISG/CHWK/dictionary/fix-fields/_v1",
					"releaseVersion": "v1"
				}
			},
			{
				"id": 24,
				"type": "group",
				"name": "AccountRatingTiersGrp",
				"element": " ",
				"displayName": "AccountRatingTiersGrp",
				"absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountRatingTiersGrp",
				"relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountRatingTiersGrp",
				"logicalName": "AccountRatingTiersGrp",
				"physicalName": "AccountRatingTiersGrp",
				"relativePaths": [
					"ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountRatingTiersGrp"
				],
				"tagAssociated": [],
				"cdeNames": [],
				"definition": "AccountRatingTiersGrp",
				"dataType": "ExternalDataTypeReleaseReference",
				"nullable": true,
				"keys": {
					"foreign": false,
					"unique": false,
					"primary": false,
					"non-unique": false
				},
				"dataTypeDefinition": {
					"typeName": "ExternalDataTypeReleaseReference",
					"business": "ISG",
					"provider": "CHWK",
					"dictionary": "fix-fields",
					"releaseFlag": false,
					"dataType": "$defs.AccountRatingTiersGrp",
					"type": "DATA_TYPE_REF",
					"absolutePath": "ISG/CHWK/dictionary/fix-fields/_v1",
					"releaseVersion": "v1"
				},
				"childAttributes": [
					{
						"id": 3192,
						"type": "element",
						"name": "AccountRatingTierType",
						"element": 10430,
						"displayName": "AccountRatingTierType",
						"absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountRatingTierType",
						"relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountRatingTierType",
						"logicalName": "AccountRatingTierType",
						"physicalName": "AccountRatingTierType",
						"relativePaths": [
							"ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountRatingTierType"
						],
						"tagAssociated": [],
						"cdeNames": [],
						"definition": "AccountRatingTierType",
						"dataType": "ExternalDataTypeReleaseReference",
						"nullable": true,
						"keys": {
							"foreign": false,
							"unique": false,
							"primary": false,
							"non-unique": false
						},
						"dataTypeDefinition": {
							"typeName": "ExternalDataTypeReleaseReference",
							"business": "ISG",
							"provider": "CHWK",
							"dictionary": "fix-fields",
							"releaseFlag": false,
							"dataType": "$defs.AccountRatingTierType",
							"type": "DATA_TYPE_REF",
							"absolutePath": "ISG/CHWK/dictionary/fix-fields/_v1",
							"releaseVersion": "v1"
						}
					}
				]
			}
		]
	}
]


[
    {
        "id": 1027,
        "type": "element",
        "name": "BrokerNumber",
        "element": 100298
    },
    {
        "id": 183,
        "type": "group",
        "name": "AccountGrp",
        "element": " ",
        "children": [
            {
                "id": 2491,
                "type": "element",
                "name": "AccountCrossType",
                "element": 10193
            },
            {
                "id": 2270,
                "type": "element",
                "name": "GrandParentAccount",
                "element": 10125
            },
            {
                "id": 2050,
                "type": "element",
                "name": "AccountRole",
                "element": 10645
            },
            {
                "id": 777,
                "type": "element",
                "name": "SettlAccountType",
                "element": 11521
            },
            {
                "id": 4376,
                "type": "element",
                "name": "AcctIDSource",
                "element": 660
            },
            {
                "id": 3357,
                "type": "element",
                "name": "RefContraBroker",
                "element": 10465
            },
            {
                "id": 1511,
                "type": "element",
                "name": "Account",
                "element": 1
            },
            {
                "id": 1381,
                "type": "element",
                "name": "MgmtGrpMnc",
                "element": 100172
            },
            {
                "id": 1970,
                "type": "element",
                "name": "LegalEntity",
                "element": 10031
            },
            {
                "id": 2489,
                "type": "element",
                "name": "AccountTransactTime",
                "element": 10191
            },
            {
                "id": 4089,
                "type": "element",
                "name": "AccountType",
                "element": 581
            },
            {
                "id": 2391,
                "type": "element",
                "name": "SUTFFlag",
                "element": 10166
            },
            {
                "id": 2783,
                "type": "element",
                "name": "NotificationEmailList",
                "element": 10271
            },
            {
                "id": 2490,
                "type": "element",
                "name": "AccountExecutionCapacity",
                "element": 10192
            },
            {
                "id": 2488,
                "type": "element",
                "name": "AccountLastMkt",
                "element": 10190
            },
            {
                "id": 2485,
                "type": "element",
                "name": "AccountCumQty",
                "element": 10187
            },
            {
                "id": 2487,
                "type": "element",
                "name": "AccountCumExecutionMUD",
                "element": 10189
            },
            {
                "id": 2486,
                "type": "element",
                "name": "AccountAvgPx",
                "element": 10188
            },
            {
                "id": 2278,
                "type": "element",
                "name": "AccountSubType",
                "element": 10133
            },
            {
                "id": 24,
                "type": "group",
                "name": "AccountRatingTiersGrp",
                "element": " ",
                "children": [
                    {
                        "id": 3192,
                        "type": "element",
                        "name": "AccountRatingTierType",
                        "element": 10430
                    },
                    {
                        "id": 3191,
                        "type": "element",
                        "name": "AccountRatingTierValue",
                        "element": 10429
                    }
                ]
            },
            {
                "id": 62,
                "type": "group",
                "name": "AltAccountGrp",
                "element": " ",
                "children": [
                    {
                        "id": 2427,
                        "type": "element",
                        "name": "AltAccountType",
                        "element": 10700
                    },
                    {
                        "id": 480,
                        "type": "element",
                        "name": "AltAccountRole",
                        "element": 11135
                    },
                    {
                        "id": 4428,
                        "type": "element",
                        "name": "AltAcctIDSource",
                        "element": 10699
                    },
                    {
                        "id": 2125,
                        "type": "element",
                        "name": "AltAccount",
                        "element": 10691
                    },
                    {
                        "id": 4319,
                        "type": "element",
                        "name": "AltAccountSubType",
                        "element": 10701
                    }
                ]
            }
        ]
    }
]














{
    "status": "SUCCESS",
    "resultBody": {
        "entityData": {
            "name": "$.body",
            "displayName": "body",
            "description": "body",
            "absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body",
            "relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body",
            "breadcrumbPath": "Information Services Group/Citihawk/dictionary/MOTradeMessage/current/body",
            "logicalName": "$.body",
            "physicalName": "$.body",
            "relativePaths": [
                "ISG/CHWK/dictionary/mo-trade-message/current/$.body"
            ],
            "links": [
            ],
            "tagAssociated": [
            ],
            "commentsAnchor": {
                "anchorUuid": "d3b2dac0-7197-3814-98e2-8e0cec6eb0d0",
                "version": "3"
            },
            "cdeAttributes": [
            ],
            "ddeAttributes": [
            ],
            "conceptType": "Child Concept",
            "attributes": [
                {
                    "name": "VSPDate",
                    "displayName": "VSPDate",
                    "absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/VSPDate",
                    "relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/VSPDate",
                    "logicalName": "VSPDate",
                    "physicalName": "VSPDate",
                    "relativePaths": [
                        "ISG/CHWK/dictionary/mo-trade-message/current/$.body/VSPDate"
                    ],
                    "tagAssociated": [
                    ],
                    "cdeNames": [
                    ],
                    "definition": "VSPDate",
                    "dataType": "ExternalDataTypeReleaseReference",
                    "nullable": true,
                    "keys": {
                        "foreign": false,
                        "unique": false,
                        "primary": false,
                        "non-unique": false
                    },
                    "dataTypeDefinition": {
                        "typeName": "ExternalDataTypeReleaseReference",
                        "business": "ISG",
                        "provider": "CHWK",
                        "dictionary": "fix-fields",
                        "releaseFlag": false,
                        "dataType": "$defs.VSPDate",
                        "type": "DATA_TYPE_REF",
                        "absolutePath": "ISG/CHWK/dictionary/fix-fields/_v1",
                        "releaseVersion": "v1"
                    }
                },
                {
                    "name": "AccountGrp",
                    "displayName": "AccountGrp",
                    "absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountGrp",
                    "relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountGrp",
                    "logicalName": "AccountGrp",
                    "physicalName": "AccountGrp",
                    "relativePaths": [
                        "ISG/CHWK/dictionary/mo-trade-message/current/$.body/AccountGrp"
                    ],
                    "tagAssociated": [
                    ],
                    "cdeNames": [
                    ],
                    "definition": "AccountGrp",
                    "dataType": "ConceptReleaseReference",
                    "nullable": true,
                    "keys": {
                        "foreign": false,
                        "unique": false,
                        "primary": false,
                        "non-unique": false
                    },
                    "dataTypeDefinition": {
                        "typeName": "ConceptReleaseReference",
                        "business": "ISG",
                        "provider": "CHWK",
                        "dictionary": "account-grp",
                        "releaseFlag": false,
                        "concept": "$",
                        "type": "CONCEPT_REF",
                        "absolutePath": "ISG/CHWK/dictionary/account-grp/_v1/$",
                        "releaseVersion": "v1"
                    }
                },
                {
                    "name": "IncompleteTradeFlag",
                    "displayName": "IncompleteTradeFlag",
                    "absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/IncompleteTradeFlag",
                    "relativePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body/IncompleteTradeFlag",
                    "logicalName": "IncompleteTradeFlag",
                    "physicalName": "IncompleteTradeFlag",
                    "relativePaths": [
                        "ISG/CHWK/dictionary/mo-trade-message/current/$.body/IncompleteTradeFlag"
                    ],
                    "tagAssociated": [
                    ],
                    "cdeNames": [
                    ],
                    "definition": "IncompleteTradeFlag",
                    "dataType": "ExternalDataTypeReleaseReference",
                    "nullable": true,
                    "keys": {
                        "foreign": false,
                        "unique": false,
                        "primary": false,
                        "non-unique": false
                    },
                    "dataTypeDefinition": {
                        "typeName": "ExternalDataTypeReleaseReference",
                        "business": "ISG",
                        "provider": "CHWK",
                        "dictionary": "fix-fields",
                        "releaseFlag": false,
                        "dataType": "$defs.IncompleteTradeFlag",
                        "type": "DATA_TYPE_REF",
                        "absolutePath": "ISG/CHWK/dictionary/fix-fields/_v1",
                        "releaseVersion": "v1"
                    }
                }
            ],
            "childConcepts": [
            ],
            "siblingItems": [
                {
                    "name": "$.body",
                    "displayName": "body",
                    "absolutePath": "ISG/CHWK/dictionary/mo-trade-message/current/$.body",
                    "entityType": "CONCEPT"
                }
            ],
            "synonyms": [
            ],
            "urn": "data://citifix-dac.citihawk.isg.icg/model/v1/mo-trade-message#/properties/body",
            "dictionaryUrn": "data://citifix-dac.citihawk.isg.icg/model/v1/mo-trade-message",
            "referencedConcepts": [
            ],
            "properties": [
                {
                    "propertyName": "isNested",
                    "code": "nested",
                    "value": [
                        "true"
                    ]
                }
            ],
            "relationships": {
                "from": [
                ],
                "to": [
                ]
            },
            "gdeAttributes": [
            ],
            "hasAdsDesignation": false
        },
        "entityType": "CONCEPT"
    },
    "statusCode": 0,
    "statusMsg": null,
    "statusDetail": {
        "serviceErrorMsg": "",
        "apiErrorMsg": "",
        "traceId": "6a7c7e6e0628de14ec431d9c15c00bca",
        "spanId": "ec431d9c15c00bca",
        "suggestion": null,
        "queryParams": null,
        "uiServicePodName": "ui-service-vep-79c669dc59-lwx7n"
    }
}

















import lombok.Data;

@Data
public class AttributeMetadata {

    private String physicalDataType;

    private String attributePhysicalName;

    private String isNullable;

    private String infoSecurityClassification;

    private String defaultValue;

    private Integer minLength;

    private Integer maxLength;

    private String unitBytes;

    private String unitChars;

    private Integer leftDecimalPrecision;

    private Integer rightDecimalPrecision;

    private String format;

    private String mask;

    private String characterEncoding;

    private String isArray;

    private Integer arrayLength;

    private String timezone;

    private String isEncrypted;

    private String displayName;

    private String provideKey;

    private String minValue;

    private String maxValue;

    private String isHidden;

    private String isFixedLength;

    private Integer fixedLengthSize;

    private String paddingSymbol;

    private String paddingAlignment;

    private String enumValues;

    private String valueType;

    private String externalCollectionReference;

    private String collectionReferenceReleaseVersion;

    private String referenceAttribute;

    private String referenceAttributeReleaseVersion;

    private String groupName;

    private Integer groupFieldOrder;
}





String downstreamResponse = callCollectionApi(requestBody);
return downstreamResponse;



import java.util.List;


private String callCollectionApi(String requestBody) {

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(
            collectionUrl,
            HttpMethod.POST,
            entity,
            String.class
    );

    return response.getBody();
}




@Override
public String createConcepts(MultipartFile file, String conceptRequestJson) throws Exception {

    ObjectMapper mapper = new ObjectMapper();

    ConceptRequest conceptRequest =
            mapper.readValue(conceptRequestJson, ConceptRequest.class);

    List<ConceptPayload> conceptPayloadList =
            convertExcelToConceptPayloadList(file);

    LOGGER.info("Concept Request: {}", conceptRequest);
    LOGGER.info("Concept Payload List size: {}", conceptPayloadList.size());

    String requestBody = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(conceptPayloadList);

    LOGGER.info("Prepared request body for collection API");

    // For now return JSON only. Later uncomment when downstream URL is confirmed.
    // String downstreamResponse = callCollectionApi(requestBody);
    // return downstreamResponse;

    return requestBody;
}





@Value("${datacatalogue.collection.url}")
private String collectionUrl;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;




package com.citi.olympus.service;

import org.springframework.web.multipart.MultipartFile;

public interface DataCatalogueService {

    String createConcepts(MultipartFile file, String conceptRequestJson) throws Exception;
}


datacatalogue.collection.url=http://localhost:8080/mock/data-catalogue/collection








@Override
public String createConcepts(MultipartFile file, String conceptRequestJson) throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    ConceptRequest conceptRequest =
            mapper.readValue(conceptRequestJson, ConceptRequest.class);

    List<ConceptPayload> conceptPayloadList =
            convertExcelToConceptPayloadList(file);

    LOGGER.info("Concept Request: {}", conceptRequest);
    LOGGER.info("Concept Payload List size: {}", conceptPayloadList.size());

    return mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(conceptPayloadList);
}
