"""
Theme class for managing theme representation in OneID Client Portal.
"""
from dataclasses import dataclass

@dataclass
class Theme:
    """
    Represents a theme with localized content.
    """
    title: str = ""
    desc: str = ""
    doc_uri: str = ""
    support_address: str = ""
    cookie_uri: str = ""