"""
LocalizedContentMap class for managing localized content in different languages.
"""
from dataclasses import dataclass, field
from typing import Dict
from theme import Theme

@dataclass
class LocalizedContentMap:
    """
    Represents a map of localized content for different languages.
    """
    content_map: Dict[str, Dict[str, Theme]] = field(
        default_factory=lambda: {
            "IT": {},
            "FR": {},
            "DE": {},
            "SL": {},
            "EN": {}
        }
    )

    def add_theme(self, language: str, key: str, theme: Theme):
        """
        Add a Theme object to the content map for a specific language and key.
        :param language: The language code (e.g., "IT", "FR").
        :param key: The key for the theme (string).
        :param theme: The Theme object to add.
        """
        if language not in self.content_map:
            raise ValueError(f"Unsupported language: {language}")
        self.content_map[language][key] = theme

    def get_theme(self, language: str, key: str) -> Theme:
        """
        Retrieve a Theme object from the content map for a specific language and key.
        :param language: The language code (e.g., "IT", "FR").
        :param key: The key for the theme (string).
        :return: The Theme object or None if not found.
        """
        return self.content_map.get(language, {}).get(key)

    def to_dynamodb(self) -> Dict:
        """
        Convert the LocalizedContentMap object into a DynamoDB-compatible representation.
        :return: A dictionary formatted for DynamoDB.
        """
        return {
                "M": {
                    language: {
                        "M": {
                            key: {
                                "M": {
                                    "title": {"S": theme.title},
                                    "desc": {"S": theme.desc},
                                    "doc_uri": {"S": theme.doc_uri},
                                    "support_address": {"S": theme.support_address},
                                    "cookie_uri": {"S": theme.cookie_uri},
                                }
                            }
                            for key, theme in themes.items()
                        }
                    }
                    for language, themes in self.content_map.items()
                }
            }
            

    @classmethod
    def from_json(cls, json_data: Dict) -> "LocalizedContentMap":
        """
        Construct a LocalizedContentMap object from a JSON payload.
        :param json_data: The JSON payload containing the content map.
        :return: A LocalizedContentMap object.
        """
        content_map = {
            language: {
                key: Theme(**theme_data)
                for key, theme_data in themes.items()
            }
            for language, themes in json_data.items()
        }
        return cls(content_map=content_map)
    

    @classmethod
    def from_dynamodb(cls, dynamodb_data: Dict) -> "LocalizedContentMap":
        """
        Construct a LocalizedContentMap object from a DynamoDB-formatted dictionary.
        :param dynamodb_data: The DynamoDB dictionary containing the content map.
        :return: A LocalizedContentMap object.
        """
        content_map = {}
        languages = dynamodb_data.get("M", {})
        for language, lang_data in languages.items():
            themes = lang_data.get("M", {})
            content_map[language] = {}
            for key, theme_data in themes.items():
                theme_fields = theme_data.get("M", {})
                content_map[language][key] = Theme(
                    title=theme_fields.get("title", {}).get("S", ""),
                    desc=theme_fields.get("desc", {}).get("S", ""),
                    doc_uri=theme_fields.get("doc_uri", {}).get("S", ""),
                    support_address=theme_fields.get("support_address", {}).get("S", ""),
                    cookie_uri=theme_fields.get("cookie_uri", {}).get("S", "")
                )
        return cls(content_map=content_map)